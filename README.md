# Pass_Task21 - Unit Converter App

A simple Android app (Jetpack Compose) for converting values across:
- Currency
- Fuel Efficiency & Distance
- Temperature

The UI uses grouped sections for **Source Selection**, **Process**, and **Conversion Result**, with destination options filtered based on the selected source unit.

---

## Features

- Source and destination unit selection with dropdown menus
- Destination list is restricted to valid conversions for the selected source
- Input validation for numeric values
- Positive-only validation for specific source units (currency and fuel/distance sources)
- Clean output formatting by category:
  - Currency: 2 decimals
  - Fuel Efficiency & Distance: 3 decimals
  - Temperature: rounded whole number

---

## Supported Conversions

## 1) Currency Conversions (Fixed 2026 Rates for Task)

Base: `USD`

- `1 USD = 1.55 AUD`
- `1 USD = 0.92 EUR`
- `1 USD = 148.50 JPY`
- `1 USD = 0.78 GBP`

Allowed route in app:
- Source: `USD`
- Destination: `AUD`, `EUR`, `JPY`, `GBP`

---

## 2) Fuel Efficiency & Distance

- `1 Mile per Gallon (mpg) = 0.425 Kilometers per Liter (km/L)`
- `1 Gallon (US) = 3.785 Liters`
- `1 Nautical Mile = 1.852 Kilometers`

Allowed routes in app:
- `mpg -> km/L`
- `Gallon (US) -> Liters`
- `Nautical Mile -> Kilometers`

---

## 3) Temperature (Climate Check)

- `F = (C * 1.8) + 32`
- `C = (F - 32) / 1.8`
- `K = C + 273.15`

Allowed routes in app:
- `Celsius -> Fahrenheit`
- `Celsius -> Kelvin`
- `Fahrenheit -> Celsius`

Display behavior:
- Temperature values are displayed with symbols in the compact button view:
  - `°C`, `°F`, `K`
- Dropdown labels use readable full names (e.g., `Celsius`, `Fahrenheit`, `Kelvin`)

---

## Input and Validation Rules

- Numeric input supports digits, decimal point, and optional leading minus (`-`) for temperature sources
- Negative values are blocked for:
  - Currency source (`USD`)
  - Fuel/Distance sources (`mpg`, `Gallon (US)`, `Nautical Mile`)
- If source input is empty or invalid, app shows an error message at destination output
- Conversion result resets when source selection changes to prevent stale output

---

## UI Notes

- Placeholder text for input is black for readability
- Source dropdown groups units by category:
  - Currency
  - Fuel Efficiency & Distance
  - Temperature
- Destination dropdown only shows units allowed for the selected source

---

## Project Structure

- Main screen and conversion logic:  
  `app/src/main/java/com/example/pass_task21/MainActivity.kt`

---

## How to Run

Open in Android Studio and run the `app` module on an emulator/device.
