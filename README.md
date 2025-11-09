
# Weather SDK for Java
**Author:** *Андрей Ведищев*  
**API Source:** [OpenWeather API](https://openweathermap.org/api)

---

## 📖 Описание

**Weather SDK** — это простая библиотека для получения данных о погоде с помощью API OpenWeatherMap.
Первоначальным источником является ресурс https://openweathermap.org.
SDK позволяет быстро интегрировать информацию о текущей погоде по названию города с обработкой кеша, поддержкой двух режимов работы (on-demand и polling).

---

## ⚙️ Основные возможности

- ✅ Поддержка инициализации с **API Key**
- 🌆 Получение **текущей погоды по названию города**
- 🕒 **Кеширование данных** (актуально в течение 10 минут)
- 🧠 Хранение информации не более чем по **10 городам**
- 🔁 Два режима работы:
    - **On-demand** — обновление данных только по запросу
    - **Polling** — периодическое фоновое обновление данных для мгновенного отклика
- 🗑️ Метод для удаления SDK-объекта
- 💾 Единый **JSON-ответ**

---

## 📦 Установка

Добавь jar-файл в свой проект (через Maven вручную в classpath).



Если используется **Maven**, добавить jar файл библиотеки в локальный репозиторий с помощью команды:

```
mvn install:install-file -Dfile=<Absolute pass> -DgroupId=openWeatherMapSdk-1.0-SNAPSHOT -DartifactId=openWeatherMapSdk-1.0-SNAPSHOT -Dversion=1.0-SNAPSHOT -Dpackaging=jar
```

пример объявления зависимости:
```xml
<dependency>
    <groupId>openWeatherMapSdk-1.0-SNAPSHOT</groupId>
    <artifactId>openWeatherMapSdk-1.0-SNAPSHOT</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Если SDK собирается вручную:
```
Перейти в IDE в меню project structure -> modules и добавить jar файл библиотеки
```

---

## 🔧 Использование

### Пример 1 — Режим On-Demand
```java
package org.example.sdk;

import com.google.gson.JsonObject;

public class ExampleUsage {
    public static void main(String[] args) {

        String apiKey = "***";
        try {
            WeatherSDK sdk = WeatherSDK.createInstance(apiKey, WeatherMode.ON_DEMAND);
            JsonObject weather = sdk.getWeather("Berlin");
            System.out.println(weather.toString());
            WeatherSDK.deleteInstance(apiKey);

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

```

---

### Пример 2 — Режим Polling
```java
/**
 * В режиме polling библиотеку имеет смысл использовать только в том случае,
 * когда она будет вызываться из другого постоянно активного потока.
 * Для примера подойдет простое веб-приложение.
 * 
 * url для вызова:
 * GET
 * http://localhost:8080/api/Berlin
 */

package com.example.SDKtest.controllers;

import com.google.gson.JsonObject;
import org.example.sdk.WeatherMode;
import org.example.sdk.WeatherSDK;
import org.example.sdk.WeatherSDKException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SdkController {

    String apiKey;
    WeatherSDK sdk;

    public SdkController() throws WeatherSDKException {
        apiKey = "***";
        sdk = WeatherSDK.createInstance(apiKey, WeatherMode.POLLING);
    }

    @GetMapping("/{city}")
    public String get(@PathVariable String city) {
        String res = "";

        try {
            JsonObject weather = sdk.getWeather(city);
            res = weather.toString();
            System.out.println(res);

        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return res;
    }
}

```

---

## 🧩 Формат JSON-ответа
Пример возвращаемого SDK объекта:
```json
{"weather":{"main":"Clouds","description":"broken clouds"},"temperature":{"temp":7.56,"feels_like":7.56},"visibility":8000,"wind":{"speed":0.89},"datetime":1762715915,"sys":{"sunrise":1762669044,"sunset":1762701783},"timezone":3600,"name":"Berlin"}
```

---



## 🧪 Что можно улучшить:

- В обоих режимах можно было бы реализовать получение информации по нескольким городам в одном запросе.
- В режиме polling реализовать получение информации по нескольким городам в одном запросе, забирая из кеша уже имеющиеся и подгружать новые, которые еще не запрашивались.

