(set-env! :dependencies  '[[org.clojure/clojure "1.8.0"]
                           [degree9/boot-npm    "1.7.0-SNAPSHOT"]
                           [degree9/boot-exec   "1.1.0-SNAPSHOT"]
                           [degree9/boot-semver "1.6.0" :scope "test"]]
          :resource-paths   #{"src"})

(require '[degree9.boot-semver :refer :all])

(task-options!
 pom    {:project 'degree9/boot-css
         :description "CSS Preprocessors for boot-clj."
         :url         "https://github.com/degree9/boot-exec"
         :scm         {:url "https://github.com/degree9/boot-exec"}})

(deftask develop
  "Build boot-css for development."
  []
  (comp
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (watch)
   (target)
   (build-jar)))

(deftask deploy
  "Build boot-css and deploy to clojars."
  []
  (comp
   (version)
   (target)
   (build-jar)
   (push-release)))
