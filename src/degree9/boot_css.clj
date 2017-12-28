(ns degree9.boot-css
  (:require [boot.core :as boot]
            [clojure.string :as s]
            [degree9.boot-exec :as exec]
            [degree9.boot-npm :as npm]))

(defn- fs-sync [tmp]
  (boot/with-pre-wrap fileset
    (apply boot/sync! tmp (boot/input-dirs fileset))
    fileset))

(boot/deftask lessc
  "CSS preprocessing using lessc."
  [l less    VAL str   "Less source file to be compiled."
   o out     VAL str   "The output file to save compiled css."]
  (let [less     (:less *opts*)
        out      (:out *opts* (s/replace less #"\.less" ".css"))
        tmp      (boot/tmp-dir!)
        tmp-path (.getAbsolutePath tmp)
        exclude  (re-pattern (str "^(.*)(?<!" out "?)$")) ;; negative look-behind
        args     [less out]]
    (comp
      (fs-sync tmp)
      (exec/exec :module "less" :process "lessc" :directory tmp-path :arguments args :include true :exclude #{exclude}))))
