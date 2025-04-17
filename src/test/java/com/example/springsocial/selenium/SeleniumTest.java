package com.example.springsocial.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;
import java.time.Duration;
import java.util.function.Function;

public class SeleniumTest {
    public static void main(String[] args) {
        // Set path to your ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver"); // Update path if needed

        // Launch browser
        WebDriver driver = new ChromeDriver();

        try {

            driver.get("http://localhost:3000");

            FluentWait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(10))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(Exception.class);

            WebElement signUpButton = wait.until(new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.linkText("Signup"));
                }
            });

            signUpButton.click();
            System.out.println(" Signup button clicked");

            Thread.sleep(1000);

            driver.findElement(By.name("name")).sendKeys("Brandon");
            Thread.sleep(1000);
            driver.findElement(By.name("email")).sendKeys("chribran@umich.edu");
            Thread.sleep(1000);
            driver.findElement(By.name("password")).sendKeys("brandon");
            Thread.sleep(1000);
            System.out.println(" Form filled");

            driver.findElement(By.xpath("//button[text()='Sign Up']")).click();

            Thread.sleep(3000);

            driver.findElement(By.name("email")).sendKeys("chribran@umich.edu");
            Thread.sleep(2000);
            driver.findElement(By.name("password")).sendKeys("brandon");
            Thread.sleep(2000);

            WebElement loginButton = driver.findElement(By.xpath("//button[text()='Login']"));
            loginButton.click();
            System.out.println(" Logged in successfully");

            Thread.sleep(5000);

            driver.navigate().refresh();

            Thread.sleep(2000);

            WebElement profileButton = driver.findElement(By.linkText("Profile"));

            profileButton.click();

            Thread.sleep(2000);

            WebElement logoutButton = driver.findElement(By.linkText("Logout"));

            logoutButton.click();

            Thread.sleep(1000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the browser
            driver.quit();
            System.out.println(" Browser closed");
        }
    }
}