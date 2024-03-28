package models

type BusinessCard struct {
   ID string `json:"id"`
   Front string `json:"front"`
   Back string `json:"back"`
   Favorite bool `json:"favorite"`
   Fields []Field `json:"fields"`
   CardType CardType `json:"cardType"`
   Template TemplateType `json:"template"`
}

type Field struct {
   Name string `json:"name"`
   Value string `json:"value"`
   Type FieldType `json:"type"`
}

type FieldType string

const (
   TEXT FieldType = "TEXT"
   URL FieldType = "URL"
   EMAIL FieldType = "EMAIL"
   PHONE_NUMBER FieldType = "PHONE_NUMBER"
   GITHUB_USERNAME FieldType = "GITHUB_USERNAME"
   LINKEDIN_ID FieldType = "LINKEDIN_ID"
)

type CardType string
const (
   PERSONAL CardType = "PERSONAL"
   SHARED CardType = "SHARED"
)

type TemplateType string

const (
   DEFAULT TemplateType = "DEFAULT"
   TEMPLATE_1 TemplateType = "TEMPLATE_1"
   TEMPLATE_2 TemplateType = "TEMPLATE_2"
   TEMPLATE_3 TemplateType = "TEMPLATE_3"
   CUSTOM TemplateType = "CUSTOM"
)
