package org.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.config.ConfigReader;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.utilities.logs.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public final class DriverFactory {

    private static final String CHROME  = "chrome";
    private static final String FIREFOX = "firefox";
    private static final String SAFARI  = "safari";
    private static final String EDGE    = "edge";

    private static final ConfigReader config = ConfigReader.getInstance();

    private DriverFactory() {
        throw new UnsupportedOperationException("DriverFactory is a utility class");
    }

    public static WebDriver createDriver() {
        String browserType = config.getBrowser().toLowerCase().trim();
        boolean isRemote   = config.isRemote();

        LogManager.info("Creating {} driver (remote={}, headless={}, strategy={})",
                browserType, isRemote, config.isHeadless(),
                config.get("page.load.strategy", "eager"));

        WebDriver driver = isRemote
                ? createRemoteDriver(browserType)
                : createLocalDriver(browserType);

        configureDriver(driver);
        LogManager.info("Driver ready — browser={}", browserType);
        return driver;
    }

    // =========================================================================
    //  LOCAL DRIVER CREATION
    // =========================================================================

    private static WebDriver createLocalDriver(String browserType) {
        return switch (browserType) {
            case CHROME  -> createChromeDriver();
            case FIREFOX -> createFirefoxDriver();
            case SAFARI  -> createSafariDriver();
            case EDGE    -> createEdgeDriver();
            default -> {
                LogManager.info("Unknown browser '{}' — defaulting to Chrome", browserType);
                yield createChromeDriver();
            }
        };
    }

    private static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = getChromeOptions();
        LogManager.debug("Launching Chrome with options: {}", options.asMap());
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = getFirefoxOptions();
        LogManager.debug("Launching Firefox with options: {}", options.asMap());
        return new FirefoxDriver(options);
    }

    private static WebDriver createSafariDriver() {
        return new SafariDriver(getSafariOptions());
    }

    private static WebDriver createEdgeDriver() {
        WebDriverManager.edgedriver().setup();
        return new EdgeDriver(getEdgeOptions());
    }

    // =========================================================================
    //  REMOTE DRIVER CREATION
    // =========================================================================

    private static WebDriver createRemoteDriver(String browserType) {
        String hubUrlString = config.getHubUrl();
        try {
            URL hubUrl = new URL(hubUrlString);
            return switch (browserType) {
                case CHROME  -> new RemoteWebDriver(hubUrl, getChromeOptions());
                case FIREFOX -> new RemoteWebDriver(hubUrl, getFirefoxOptions());
                case SAFARI  -> new RemoteWebDriver(hubUrl, getSafariOptions());
                case EDGE    -> new RemoteWebDriver(hubUrl, getEdgeOptions());
                default      -> new RemoteWebDriver(hubUrl, getChromeOptions());
            };
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Selenium Grid hub URL: " + hubUrlString, e);
        }
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        String strategy = config.get("page.load.strategy", "eager").toLowerCase();
        options.setPageLoadStrategy(switch (strategy) {
            case "normal" -> PageLoadStrategy.NORMAL;
            case "none"   -> PageLoadStrategy.NONE;
            default       -> PageLoadStrategy.EAGER;   // ← FIX 1
        });

        // ── Headless mode ─────────────────────────────────────────────────────
        if (config.isHeadless()) {
            options.addArguments("--headless=new");    // modern headless (Chrome 112+)
            options.addArguments("--window-size=1920,1080");
        }

        // ── Base stability / anti-bot flags (always applied) ─────────────────
        options.addArguments(
                "--no-sandbox",                            // required for most CI environments
                "--disable-dev-shm-usage",                 // prevents /dev/shm crashes on Linux
                "--disable-gpu",                           // avoids GPU process issues
                "--disable-extensions",                    // no extension interference
                "--disable-infobars",                      // hide "Chrome is being controlled" bar
                "--disable-notifications",                 // no pop-up permission prompts
                "--disable-popup-blocking",                // allow all popups (for tests)
                "--start-maximized",                       // full-size window
                "--remote-allow-origins=*",               // required for Selenium 4 DevTools Protocol
                "--disable-blink-features=AutomationControlled"  // hides automation flag from site
        );

        options.addArguments(
                "--disable-renderer-backgrounding",        // never suspend renderer tabs
                "--disable-backgrounding-occluded-windows",// keep renderer alive when covered
                "--disable-features=RendererCodeIntegrity",// prevents renderer crash on Windows
                "--force-device-scale-factor=1",           // consistent resolution
                "--disable-ipc-flooding-protection"        // prevent IPC message rate-limiting
        );

        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        addArgsFromProperties(options, "chrome.options");

        return options;
    }

    // =========================================================================
    //  FIREFOX OPTIONS
    // =========================================================================

    private static FirefoxOptions getFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();

        // ── Page load strategy ────────────────────────────────────────────────
        String strategy = config.get("page.load.strategy", "eager").toLowerCase();
        options.setPageLoadStrategy(switch (strategy) {
            case "normal" -> PageLoadStrategy.NORMAL;
            case "none"   -> PageLoadStrategy.NONE;
            default       -> PageLoadStrategy.EAGER;   // ← FIX 1
        });

        // ── Headless mode ─────────────────────────────────────────────────────
        if (config.isHeadless()) {
            options.addArguments("--headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
        }

        FirefoxProfile profile = new FirefoxProfile();

        // Suppress notification prompts
        profile.setPreference("dom.webnotifications.enabled", false);
        profile.setPreference("dom.push.enabled", false);
        profile.setPreference("dom.disable_beforeunload", true);

        // Suppress geolocation prompt
        profile.setPreference("geo.enabled", false);
        profile.setPreference("geo.prompt.testing", false);

        // Suppress media auto-play
        profile.setPreference("media.volume_scale", "0.0");
        profile.setPreference("media.autoplay.default", 5);

        // Suppress download dialog
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/pdf,application/octet-stream,application/x-winzip,application/pdf");
        profile.setPreference("pdfjs.disabled", true);

        // Performance: disable unnecessary background services
        profile.setPreference("browser.tabs.remote.autostart", false);
        profile.setPreference("extensions.logging.enabled", false);
        profile.setPreference("network.http.connection-timeout", 60);
        profile.setPreference("network.http.response.timeout", 60);

        options.setProfile(profile);

        // ── FIX 2: Read extra args from firefox.options in properties file ────
        // qa.properties:  firefox.options=--disable-blink-features=AutomationControlled
        addArgsFromFirefoxOptions(options, "firefox.options");

        return options;
    }

    // =========================================================================
    //  SAFARI / EDGE OPTIONS
    // =========================================================================

    private static SafariOptions getSafariOptions() {
        SafariOptions options = new SafariOptions();
        options.setAutomaticInspection(false);
        options.setAutomaticProfiling(false);
        return options;
    }

    private static EdgeOptions getEdgeOptions() {
        EdgeOptions options = new EdgeOptions();

        String strategy = config.get("page.load.strategy", "eager").toLowerCase();
        options.setPageLoadStrategy(switch (strategy) {
            case "normal" -> PageLoadStrategy.NORMAL;
            case "none"   -> PageLoadStrategy.NONE;
            default       -> PageLoadStrategy.EAGER;
        });

        if (config.isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-extensions",
                "--start-maximized",
                "--disable-renderer-backgrounding",
                "--remote-allow-origins=*"
        );

        addArgsFromEdgeOptions(options, "edge.options");
        return options;
    }



    private static void configureDriver(WebDriver driver) {
        int implicitWait = config.getImplicitWaitTimeout();   // from properties: 10s
        int pageLoad     = config.getPageLoadTimeout();       // from properties: 60s
        int scriptTimeout = config.getInt("script.timeout", 30);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoad));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(scriptTimeout));

        if (!config.isHeadless()) {
            try {
                driver.manage().window().maximize();
            } catch (Exception ignored) { /* non-fatal */ }
        }

        LogManager.debug("Driver configured — implicit={}s, pageLoad={}s, script={}s, strategy={}",
                implicitWait, pageLoad, scriptTimeout,
                config.get("page.load.strategy", "eager"));
    }

    private static void addArgsFromProperties(ChromeOptions options, String propertyKey) {
        String rawArgs = config.get(propertyKey);
        if (rawArgs == null || rawArgs.trim().isEmpty()) return;

        List<String> args = Arrays.stream(rawArgs.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (!args.isEmpty()) {
            options.addArguments(args);
            LogManager.debug("Applied {} extra Chrome args from {}: {}", args.size(), propertyKey, args);
        }
    }

    private static void addArgsFromFirefoxOptions(FirefoxOptions options, String propertyKey) {
        String rawArgs = config.get(propertyKey);
        if (rawArgs == null || rawArgs.trim().isEmpty()) return;

        Arrays.stream(rawArgs.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(arg -> {
                    options.addArguments(arg);
                    LogManager.debug("Applied Firefox arg from {}: {}", propertyKey, arg);
                });
    }

    private static void addArgsFromEdgeOptions(EdgeOptions options, String propertyKey) {
        String rawArgs = config.get(propertyKey);
        if (rawArgs == null || rawArgs.trim().isEmpty()) return;

        List<String> args = Arrays.stream(rawArgs.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (!args.isEmpty()) {
            options.addArguments(args);
        }
    }


    /** Creates a driver with a browser type that overrides the properties file value. */
    public static WebDriver createDriver(String browserType) {
        LogManager.info("Creating driver with browser override: {}", browserType);
        WebDriver driver = createLocalDriver(browserType.toLowerCase().trim());
        configureDriver(driver);
        return driver;
    }

    public static WebDriver createHeadlessDriver(String browserType) {
        LogManager.info("Creating forced-headless driver for: {}", browserType);
        WebDriver driver = switch (browserType.toLowerCase().trim()) {
            case CHROME -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = getChromeOptions();
                opts.addArguments("--headless=new", "--window-size=1920,1080");
                yield new ChromeDriver(opts);
            }
            case FIREFOX -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = getFirefoxOptions();
                opts.addArguments("--headless");
                yield new FirefoxDriver(opts);
            }
            case EDGE -> {
                WebDriverManager.edgedriver().setup();
                EdgeOptions opts = getEdgeOptions();
                opts.addArguments("--headless=new");
                yield new EdgeDriver(opts);
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = getChromeOptions();
                opts.addArguments("--headless=new", "--window-size=1920,1080");
                yield new ChromeDriver(opts);
            }
        };
        configureDriver(driver);
        return driver;
    }
}