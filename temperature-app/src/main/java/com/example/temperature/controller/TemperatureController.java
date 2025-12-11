package com.example.temperature.controller;

import com.example.temperature.dto.WeatherResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TemperatureController {
    
    private static final String API_KEY = "71c2cb414d2befe91ec95055b20b4341";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?q={city}&appid={apiKey}&units=metric&lang=ru";
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @GetMapping("/")
    public String getTemperature(
            @RequestParam(name = "city", required = false, defaultValue = "Moscow") String city,
            Model model) {
        
        Map<String, Object> weatherData = new HashMap<>();
        boolean error = false;
        String errorMessage = "";
        
        try {
            // Вызов API OpenWeatherMap
            Map<String, String> params = new HashMap<>();
            params.put("city", city);
            params.put("apiKey", API_KEY);
            
            String response = restTemplate.getForObject(WEATHER_API_URL, String.class, params);
            WeatherResponse weatherResponse = objectMapper.readValue(response, WeatherResponse.class);
            
            // Преобразование timestamp в читаемую дату
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(weatherResponse.getTimestamp()),
                ZoneId.systemDefault()
            );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            String currentTime = dateTime.format(formatter);
            
            // Определяем иконку погоды
            String weatherIcon = getWeatherIcon(weatherResponse.getWeather()[0].getId());
            String weatherDescription = capitalizeFirstLetter(weatherResponse.getWeather()[0].getDescription());
            
            // Подготовка данных для отображения
            weatherData.put("city", weatherResponse.getName());
            weatherData.put("temperature", Math.round(weatherResponse.getMain().getTemp()));
            weatherData.put("feelsLike", Math.round(weatherResponse.getMain().getFeelsLike()));
            weatherData.put("tempMin", Math.round(weatherResponse.getMain().getTempMin()));
            weatherData.put("tempMax", Math.round(weatherResponse.getMain().getTempMax()));
            weatherData.put("humidity", weatherResponse.getMain().getHumidity());
            weatherData.put("pressure", weatherResponse.getMain().getPressure());
            weatherData.put("windSpeed", weatherResponse.getWind().getSpeed());
            weatherData.put("description", weatherDescription);
            weatherData.put("weatherIcon", weatherIcon);
            weatherData.put("currentTime", currentTime);
            weatherData.put("country", weatherResponse.getSys().getCountry());
            
        } catch (Exception e) {
            error = true;
            errorMessage = "Не удалось получить данные для города: " + city + ". Проверьте правильность названия.";
            weatherData.put("city", city);
            weatherData.put("temperature", "N/A");
            weatherData.put("description", "Данные недоступны");
            weatherData.put("weatherIcon", "❓");
        }
        
        model.addAttribute("weather", weatherData);
        model.addAttribute("error", error);
        model.addAttribute("errorMessage", errorMessage);
        
        return "temperature";
    }
    
    @GetMapping("/api/temperature")
    public Map<String, Object> getTemperatureApi(
            @RequestParam(name = "city", required = false, defaultValue = "Moscow") String city) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, String> params = new HashMap<>();
            params.put("city", city);
            params.put("apiKey", API_KEY);
            
            String apiResponse = restTemplate.getForObject(WEATHER_API_URL, String.class, params);
            WeatherResponse weatherResponse = objectMapper.readValue(apiResponse, WeatherResponse.class);
            
            response.put("city", weatherResponse.getName());
            response.put("country", weatherResponse.getSys().getCountry());
            response.put("temperature", Math.round(weatherResponse.getMain().getTemp()));
            response.put("feels_like", Math.round(weatherResponse.getMain().getFeelsLike()));
            response.put("temp_min", Math.round(weatherResponse.getMain().getTempMin()));
            response.put("temp_max", Math.round(weatherResponse.getMain().getTempMax()));
            response.put("humidity", weatherResponse.getMain().getHumidity());
            response.put("pressure", weatherResponse.getMain().getPressure());
            response.put("wind_speed", weatherResponse.getWind().getSpeed());
            response.put("description", weatherResponse.getWeather()[0].getDescription());
            response.put("timestamp", weatherResponse.getTimestamp());
            response.put("unit", "Celsius");
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to fetch weather data for city: " + city);
            response.put("city", city);
        }
        
        return response;
    }
    
    private String getWeatherIcon(int weatherId) {
        if (weatherId >= 200 && weatherId < 300) {
            return "⛈️"; // Гроза
        } else if (weatherId >= 300 && weatherId < 400) {
            return "🌧️"; // Морось
        } else if (weatherId >= 500 && weatherId < 600) {
            return "🌧️"; // Дождь
        } else if (weatherId >= 600 && weatherId < 700) {
            return "❄️"; // Снег
        } else if (weatherId >= 700 && weatherId < 800) {
            return "🌫️"; // Атмосферные явления
        } else if (weatherId == 800) {
            return "☀️"; // Ясно
        } else if (weatherId == 801) {
            return "🌤️"; // Малооблачно
        } else if (weatherId == 802) {
            return "⛅"; // Облачно с прояснениями
        } else if (weatherId == 803 || weatherId == 804) {
            return "☁️"; // Пасмурно
        } else {
            return "🌡️";
        }
    }
    
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}