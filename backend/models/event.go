package models

type Event struct {
  ID string `json:"id"`
  Name string `json:"name"`
  Location string `json:"location"`
  StartDate string `json:"startDate"`
  EndDate string `json:"endDate"`
  NumUsers int `json:"numUsers"`
  MaxUsers int `json:"maxUsers"`
  MaxUsersSet bool `json:"maxUsersSet"`
  EventType string `json:"eventType"`
}

type EventCards []BusinessCard
