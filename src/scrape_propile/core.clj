(ns scrape-propile.core
  (:require [clj-webdriver.taxi :as w]))

(def scraper-dir (str (System/getProperty "java.io.tmpdir")
                         "/scrape_propile"))
(def temp-dir     (str scraper-dir "/temp"))
(def download-dir (str scraper-dir "/downloads"))
(def log-file     (str scraper-dir "/log"))

(defn request-user-pass []
  (let [get-input (fn [p] (print p) (flush) (read-line))
        get-pass (fn [p] (print p) (flush) (read-line))]
    ;get-pass (fn [p] (print p) (flush) (apply str (. (. System console) readPassword)))]
    [(get-input "Username: ") (get-pass "Password: ")]))

(defn login [user pass]
  (w/input-text "#account_email" user)
  (w/input-text "#account_password" pass)
  (w/click "input[name=commit]"))

(defn start-browser []
  (w/set-driver! (clj-webdriver.core/new-driver
                 {:browser :firefox :profile
                           (doto (clj-webdriver.firefox/new-profile)
                             ;; Auto-download PDFs to a specific folder
                             (clj-webdriver.firefox/set-preferences
                               {:browser.download.dir download-dir,
                                :browser.download.folderList 2
                                :browser.helperApps.neverAsk.saveToDisk "application/pdf"
                                :pdfjs.disabled true}))})))

(defn create-directories [& dirs]
  (doseq [d dirs]
    (.mkdir (java.io.File. d))))

(defn visit-sessions []
  (let [session-count (count (w/elements "table:first-of-type td:nth-child(3) a"))]
    (doseq [link-no (range 2 (+ 2 session-count))]
      (w/click (str "table:first-of-type tr:nth-of-type(" link-no ") td:nth-child(3) a"))
      (w/click ".submenu a:nth-of-type(3)")
      (w/back))))

(defn initialize-browser []
  (let [[user pass] (request-user-pass)]
    (println "Creating temporary directories if necessary.")
    (create-directories scraper-dir temp-dir download-dir)
    (println "Starting browser.")
    (start-browser)
    (println "Logging in.")
    (w/to "https://call4paper-agileconf.herokuapp.com/account/session/new")
    (login user pass)
    (w/click "a[href='/sessions']")))

(defn -main []
  (initialize-browser)
  (visit-sessions))
