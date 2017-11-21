(ns degree9.boot-css
  (:require [boot.core :as boot]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [boot.util :as u]
            [degree9.boot-npm :as npm]))

(defn compile-less-file!
  [fs tmp-dir tmp-file & [{:keys [include-dirs]}]]
  (let [compiled-file (io/file tmp-dir (str/replace (boot/tmp-path tmp-file) #"\.less$" ".css"))
        ]
    (u/dbug* "Less boot dir: %s\n" (boot/tmp-dir tmp-file))
    (u/dbug* "Less boot file: %s\n" (-> tmp-file boot/tmp-file .getAbsolutePath))
    (u/dbug* "Less boot target: %s\n" (.getAbsolutePath compiled-file))
    ;; boot magic, we trigger the task for side effects targeting the absolute
    ;; folders only.
    (((npm/exec :module "less"
                :process "lessc"
                :arguments (->> [(when (seq include-dirs)
                                   (str "--include-path=" include-dirs))
                                 (-> tmp-file boot/tmp-file .getAbsolutePath)
                                 (.getAbsolutePath compiled-file)]
                                (keep identity)
                                vec))
      identity) fs)))

(boot/deftask lessc
  "Compile CSS using the npm version of Less."
  [l less-file    PATH  str    "Path to the .less file to compile (relative to the fileset)."
   d less-dir     PATH  str    "Path to a dir containing .less files."
   i include-dirs PATH  #{str} "Include directories for lessc."]
  (assert (or less-file less-dir) "Either a less-file or less-dir should be present for this task to work.")
  (assert (not (and less-file less-dir)) "The less-file and less-dir parameters are mutually exclusive.")

  (let [tmp (boot/tmp-dir!)]
    (boot/with-pre-wrap [fs]
      (let [less-dirs (when less-dir
                        (->> fs
                             boot/ls
                             (boot/by-re [(re-pattern (str less-dir ".*\\.less$"))])
                             seq))
            less-tmp-files (->> (or less-dirs #{})
                                (into [(when less-file (boot/tmp-get fs less-file))])
                                (keep identity)
                                vec)
            include-dirs (->> less-tmp-files
                              (mapv #(boot/tmp-dir %))
                              (into (or include-dirs #{}))
                              (str/join ":"))]
        (u/dbug* "Less TmpFiles: %s\n" less-tmp-files)

        (assert (seq less-tmp-files)
                (format "Cannot find less files on the classpath, review your parameters (less-file=%s, less-dir=%s)." less-file less-dir))
        (run! #(compile-less-file! fs tmp % {:include-dirs include-dirs})
              less-tmp-files)
        (-> fs (boot/add-resource tmp) boot/commit!)))))
