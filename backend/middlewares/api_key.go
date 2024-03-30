package middlewares

import (
	"net/http"
	"os"
)

func APIKey(next http.Handler) http.Handler {
  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
    env_api_key := os.Getenv("API_KEY")

    header_api_key := r.Header.Get("X-API-KEY")
    query_key := r.URL.Query().Get("key")

    if header_api_key == env_api_key || query_key == env_api_key {
      next.ServeHTTP(w, r)
    } else {
      http.Error(w, "Unauthorized", http.StatusUnauthorized)
    }
  })
}
