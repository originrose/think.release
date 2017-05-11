# think.release

Library for manipulating hierarchies of project files to enable releasing and updating version
efficiently.


project.clj:

```clojure
  :profiles {:tools {:plugins [[lein-environ "1.1.0"]]
                     :dependencies [[thinktopic/think.release "0.1.0-2017-05-11-14-13"]]
                     :env {:date-version "false"}}}

  :aliases {"release" ["with-profile" "tools" "run" "-m" "think.release.main"]}
```

example release [script](examples/release.sh)

## License

Copyright © 2017 ThinkTopic.com, LLC

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
