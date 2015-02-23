(ns kixi.nhs.exploration-ui.dev
  (:require [kixi.nhs.exploration-ui.ui :as ui]
            [figwheel.client :as figwheel :include-macros true]
            [cljs.core.async :refer [put!]]
            [weasel.repl :as weasel]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn []
                     (ui/main)))

(weasel/connect "ws://localhost:9001" :verbose true :print #{:repl :console})

(ui/main)
