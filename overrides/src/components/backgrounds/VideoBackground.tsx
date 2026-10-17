import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import strings from "../../constants/strings";
import { resolveVideoUrl } from "../../lib/assetPacks";

type Props = {
  src: string;
  audioEnabled: boolean;
  objectPosition?: string;
};

type LoadingOverlayProps = {
  received: number;
  total: number;
  progress: number;
  indeterminate: boolean;
};

const formatMB = (bytes: number) => (bytes / 1_048_576).toFixed(1);

const LoadingOverlay = ({
  received,
  total,
  progress,
  indeterminate,
}: LoadingOverlayProps) => (
  <div className="absolute inset-0 flex items-center justify-center overflow-hidden">
    <div className="relative flex flex-col items-center gap-4">
      <p className="font-pixel text-crown-gold tracking-widest text-lg">
        {strings.VIDEO_BACKGROUND_DOWNLOADING_TEXT}
      </p>
      <div className="w-48 h-3 border-2 border-obsidian-600 bg-obsidian-800 overflow-hidden">
        <div
          className={
            indeterminate
              ? "h-full bg-crown-gold animate-pulse w-full"
              : "h-full bg-crown-gold transition-all duration-150"
          }
          style={indeterminate ? undefined : { width: `${progress}%` }}
        />
      </div>
      <p className="font-pixel text-obsidian-500 text-sm tracking-widest">
        {indeterminate
          ? strings.VIDEO_BACKGROUND_SIZE_TEXT(formatMB(received))
          : strings.VIDEO_BACKGROUND_PROGRESS_TEXT(
              formatMB(received),
              formatMB(total)
            )}
      </p>
    </div>
  </div>
);

export const VideoBackground = ({
  src,
  audioEnabled,
  objectPosition = "50% 50%",
}: Props) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [objectUrl, setObjectUrl] = useState<string | null>(null);
  const [progress, setProgress] = useState(0);
  const [received, setReceived] = useState(0);
  const [total, setTotal] = useState(0);
  const [indeterminate, setIndeterminate] = useState(false);
  const [hasError, setHasError] = useState(false);
  const [prevSrc, setPrevSrc] = useState(src);

  if (src !== prevSrc) {
    setPrevSrc(src);
    setObjectUrl(null);
    setProgress(0);
    setReceived(0);
    setTotal(0);
    setIndeterminate(false);
    setHasError(false);
  }

  useEffect(() => {
    let cancelled = false;
    let createdUrl: string | null = null;
    const controller = new AbortController();

    const load = async () => {
      try {
        const resolvedSrc = await resolveVideoUrl(
          src,
          ({ received: packReceived, total: packTotal }) => {
            if (cancelled) return;
            setReceived(packReceived);
            if (packTotal > 0) {
              setTotal(packTotal);
              setIndeterminate(false);
              setProgress(
                Math.min(99, Math.round((packReceived / packTotal) * 100))
              );
            } else {
              setIndeterminate(true);
            }
          },
          controller.signal
        );
        if (cancelled) return;

        const response = await fetch(resolvedSrc);
        if (cancelled) return;
        if (!response.ok || !response.body) {
          setHasError(true);
          return;
        }

        const totalHeader = response.headers.get("content-length");
        const totalBytes = totalHeader ? parseInt(totalHeader, 10) : 0;
        if (!totalBytes) {
          setIndeterminate(true);
        } else {
          setTotal(totalBytes);
        }

        const reader = response.body.getReader();
        const chunks: BlobPart[] = [];
        let receivedBytes = 0;

        while (true) {
          const { done, value } = await reader.read();
          if (cancelled) return;
          if (done) break;
          if (value) {
            chunks.push(value);
            receivedBytes += value.length;
            setReceived(receivedBytes);
            if (totalBytes)
              setProgress(
                Math.min(99, Math.round((receivedBytes / totalBytes) * 100))
              );
          }
        }

        const blob = new Blob(chunks, {
          type: response.headers.get("content-type") || "video/mp4",
        });
        createdUrl = URL.createObjectURL(blob);

        if (cancelled) {
          URL.revokeObjectURL(createdUrl);
          return;
        }
        setProgress(100);
        setObjectUrl(createdUrl);
      } catch {
        if (!cancelled) setHasError(true);
      }
    };

    void load();

    return () => {
      cancelled = true;
      controller.abort();
      if (createdUrl) URL.revokeObjectURL(createdUrl);
    };
  }, [src]);

  useEffect(() => {
    const video = videoRef.current;
    if (!video || !objectUrl) return;

    const unlockAudio = () => {
      if (!audioEnabled) return;
      video.muted = false;
      video.play().catch(() => {});
    };

    if (audioEnabled) {
      video.muted = false;
      video.play().catch(() => {
        video.muted = true;
        video.play().catch(() => {});
      });
    } else {
      video.muted = true;
      video.play().catch(() => {});
    }

    document.addEventListener("pointerdown", unlockAudio);
    document.addEventListener("keydown", unlockAudio);
    document.addEventListener("touchstart", unlockAudio, { passive: true });

    const onVisibilityChange = () => {
      if (document.hidden) video.pause();
      else video.play().catch(() => {});
    };
    document.addEventListener("visibilitychange", onVisibilityChange);

    return () => {
      document.removeEventListener("pointerdown", unlockAudio);
      document.removeEventListener("keydown", unlockAudio);
      document.removeEventListener("touchstart", unlockAudio);
      document.removeEventListener("visibilitychange", onVisibilityChange);
    };
  }, [audioEnabled, objectUrl]);

  return (
    <div
      className="fixed inset-0 pointer-events-none overflow-hidden"
      style={{ background: "#0d1322", zIndex: 0 }}
    >
      {objectUrl && (
        <video
          ref={videoRef}
          src={objectUrl}
          autoPlay
          loop
          muted
          playsInline
          className="w-full h-full object-cover"
          style={{ objectPosition }}
        />
      )}
      {!objectUrl &&
        !hasError &&
        createPortal(
          <div
            className="fixed inset-0 pointer-events-none"
            style={{ background: "#0d1322", zIndex: 30 }}
          >
            <LoadingOverlay
              received={received}
              total={total}
              progress={progress}
              indeterminate={indeterminate}
            />
          </div>,
          document.body
        )}
    </div>
  );
};
