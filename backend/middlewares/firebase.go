package middlewares

import (
	"context"
	"net/http"

	"cloud.google.com/go/firestore"
	"firebase.google.com/go/storage"
)

type Firebase struct {
  FirestoreClient *firestore.Client
  StorageClient *storage.Client
}

func (f *Firebase) LoadContext(next http.Handler) http.Handler {
  return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
    ctx := context.WithValue(r.Context(), "firestore", f.FirestoreClient)
    ctx = context.WithValue(ctx, "storage", f.StorageClient)
    r = r.WithContext(ctx)
    next.ServeHTTP(w, r)
  })
}
