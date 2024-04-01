package middlewares

import (
	"fmt"
	"net/http"
	"time"

	"github.com/rs/zerolog/log"
)

type wrappedWriter struct {
   http.ResponseWriter
   StatusCode int
}

func (w *wrappedWriter) WriteHeader(statusCode int) {
   w.ResponseWriter.WriteHeader(statusCode)
   w.StatusCode = statusCode
}

func Logging(next http.Handler) http.Handler {
  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
    start := time.Now()

    wrapped := &wrappedWriter{
      ResponseWriter: w,
      StatusCode: http.StatusOK,
    }

    next.ServeHTTP(w, r)
    msg := fmt.Sprintf("%d %s %s - (%s)", wrapped.StatusCode ,r.Method, r.URL.Path, time.Since(start))
    log.Info().Msg(msg)
  })
}
