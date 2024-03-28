package models

type Event struct {
  ID string `json:"id"`
  Location string `json:"location"`
  NumUsers int `json:"numUsers"`
  MaxUsers int `json:"maxUsers"`
}

type EventCards []BusinessCard
