package main

import (
	"fmt"
	"net/http"
	"os"

	"github.com/rs/zerolog/log"
)


func loggingMiddleware(next http.Handler) http.Handler {
  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
    msg := fmt.Sprintf("%s: %s", r.Method, r.RequestURI)
    log.Info().Msg(msg)
    next.ServeHTTP(w, r)
  })
}

func apiKeyAuthMiddleware(next http.Handler) http.Handler {
  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
    supplied_api_key := r.Header.Get("X-API-KEY")
    env_api_key := os.Getenv("API_KEY")
    if supplied_api_key == env_api_key {
      next.ServeHTTP(w, r)
    } else {
      http.Error(w, "NO AUTH", http.StatusUnauthorized)
    }
  })
}
