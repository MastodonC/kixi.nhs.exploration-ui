(defproject kixi/nhs.exploration-ui "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :test-paths ["test/clj" "test/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2496" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.2"]
                 [compojure "1.3.1"]
                 [enlive "1.1.5"]
                 [environ "1.0.0"]

                 ;; cljs
                 [org.omcljs/om "0.8.8"]
                 [sablono "0.3.1"]
                 [kixi/charts "0.1.0-SNAPSHOT"]
                 [cljs-ajax "0.3.10"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.5.0"

  :uberjar-name "kixi.nhs.exploration-ui.jar"

  :clean-targets ^{:protect false} ["target" "resources/public/js"]

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :preamble      ["react/react.min.js"]
                                        :externs       ["react/externs/react.js"]
                                        :optimizations :none
                                        :pretty-print  true}}
                       :test {:source-paths ["src/cljs" "test/cljs"]
                              :compiler {:output-to "target/testable.js"
                                         :optimizations :whitespace
                                         :pretty-print  true}}}
              :test-commands {"test" ["phantomjs" "phantom/unit-test.js" "phantom/unit-test.html"]}}

  :profiles {:dev {:source-paths ["env/dev/clj"]

                   :dependencies [[figwheel "0.1.6-SNAPSHOT"]
                                  [com.cemerick/piggieback "0.1.3"]
                                  [weasel "0.4.2"]
                                  [leiningen "2.5.0"]]

                   :repl-options {:init-ns kixi.nhs.exploration-ui.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :plugins [[lein-figwheel "0.1.6-SNAPSHOT"]]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]}

                   :env {:is-dev true}

                   :cljsbuild {:builds
                               {:app
                                {:source-paths ["env/dev/cljs"]}}}}

             :uberjar {:source-paths ["env/prod/clj"]
                       :hooks [leiningen.cljsbuild  ]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
