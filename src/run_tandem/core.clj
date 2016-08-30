(ns run-tandem.core
  (:require [me.raynes.fs :as fs]
            [clj-tandem.core :refer [default-inputs xtandem]]
            [clj-commons-exec :refer [sh]]
            [clojure.java.io :refer [reader file writer]]
            [clojure.string :as st]
            [clojure.edn :as edn]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; cli
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cli-options
  [["-l" "--files PATH" "Path to file containing paths of mzML or mgf files to be searched."
    :parse-fn #(fs/absolute (file %))
    :validate [#(fs/exists? %)
               "File list does not exist."]]
   ["-f" "--fasta PATH" "Path to fasta file to be used in searches."
    :parse-fn #(fs/absolute (file %))
    :validate [#(fs/exists? %)
               "Fasta file does not exist."]]
   ["-p" "--params PATH" "OPTIONAL: Path to file containing X! Tandem parameters."
    :parse-fn #(fs/absolute (file %))
    :validate [#(fs/exists? %)
               "Parameter file does not exist."]]
   ["-d" "--defaults" "Print default parameters used in searches."]
   ["-h" "--help" "Print help message."]])

(defn usage [options-summary]
  (->> ["Simple utility for running X! Tandem on the command line.
  Takes a text file containing paths to spectra files to be used in
  the searches (one per line; -i), a FASTA database (-f) and a text
  file containing X! Tandem parameters one per line. For example,
  \"spectrum, parent monoisotopic mass error units=ppm\". Default
  parameters assume using the TPP X! Tandem, i.e. k-score scoring, but
  this can be changed using the parameter file. Default parameters can
  be printed using the '-d' flag. Expects to find a 'tandem'
  executable in the PATH variable. Output file is the name of the
  spectra file with '.tandem.xml' replacing the extension."
        ""
        "Usage: sg-protein-search [options]"
        ""
        "Options:"
        options-summary
        ""]
       (st/join \newline)))

(defn error-msg [errors]
  (str "Error:\n"
       (->> errors
            (interpose \newline)
            (apply str))))

(defn exit [status msg]
  (println msg)
  (System/exit status))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; processing parameters
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn clean-line
  [line]
  (when-not (or (= "" (st/trim line))
                (= \# (first line)))
    (let [f (st/split line #"=")]
      (when-not (= 2 (count f))
        (throw (Exception.
                (str "Malformed parameter entry in parameter file: " line))))
      (vec f))))

(defn process-parameters
  [file]
  (if file
    (with-open [r (reader file)]
      (into {} (map clean-line (line-seq r))))))

(defn process-files
  [f]
  (with-open [r (reader f)]
    (let [fs (doall (->> (map st/trim (line-seq r))
                         (filter #(not (or (= "" %) (= \# (first %)))))
                         (map file)))]
      (if-let [n (some #(not (fs/exists? %)) fs)]
        (exit 1 (str "Input file '" (str (fs/absolute n)) "' does not exist."))
        fs))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; default display
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn print-defaults
  []
  (let [d (as-> (default-inputs) d
            (dissoc d "list path, taxonomy information" "output, sequence path")
            (map (fn [[k v]] (str k ": " v)) d)
            (group-by #(or (second (re-find #"^([^,]+)," %))
                           (first (st/split % #":"))) d))]
    (println "Default parameters for X! Tandem search:")
    (println)
    (doseq [[k v] d]
      (println (str "** " (st/capitalize k) " parameters:"))
      (doseq [p v]
        (println p))
      (println))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; main
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options)
      (exit 0 (usage summary))
      errors
      (exit 1 (error-msg errors)))
    ;; Execute program with options
    (cond (:defaults options)
          (print-defaults)
          :else
          (let [params (process-parameters (:params options))]
            (cond (not (:files options))
                  (exit 1  (str \newline
                                "ERROR: Must specify a file list file."
                                \newline \newline
                                (usage summary)))
                  (not (:fasta options))
                  (exit 1  (str \newline
                                "ERROR: Must specify a FASTA file."
                                \newline \newline
                                (usage summary)))
                  :else
                  (doseq [f (process-files (:files options))]
                    (println "Searching " (fs/base-name f) " ...")
                    (xtandem (:fasta options) (str f) params)))))))

