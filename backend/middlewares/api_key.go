package middlewares

import (
	"net/http"
	"os"
)

func APIKey(next http.Handler) http.Handler {
  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
    supplied_api_key := r.Header.Get("X-API-KEY")
    env_api_key := os.Getenv("API_KEY")
    if supplied_api_key == env_api_key {
      next.ServeHTTP(w, r)
    } else {
      http.Error(w, "Unauthorized", http.StatusUnauthorized)
    }
  })
}
