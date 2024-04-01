package handlers

import (
	"io"
	"net/http"
)

func Index(w http.ResponseWriter, r *http.Request) {
   w.WriteHeader(http.StatusOK)
   io.WriteString(w, "Working!")
}
