package logger

import (
	"context"
	"log/slog"
)

const loggerContextKey = "logger"

func FromContext(ctx context.Context) *slog.Logger {
	if logger, ok := ctx.Value(loggerContextKey).(*slog.Logger); ok {
		return logger
	}

	return slog.Default()
}

func WithContext(ctx context.Context, logger *slog.Logger) context.Context {
	return context.WithValue(ctx, loggerContextKey, logger)
}
