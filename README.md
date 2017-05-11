# think.release

Library for manipulating hierarchies of project files to enable releasing and updating version
efficiently.


## Used in your project as a replacement for lein release

* In your project.clj:

```clojure
  :profiles {:tools {:plugins [[lein-environ "1.1.0"]]
                     :dependencies [[thinktopic/think.release "0.1.0-2017-05-11-14-13"]]
                     :env {:date-version "false"}}}

  :aliases {"release" ["with-profile" "tools" "run" "-m" "think.release.main"]}
```

* example release [script](examples/release.sh)



## Used as a command line program:

```
scripts[master] % lein run
commands:
:show-snapshot-version: Show the pending snapshot version.
:set-snapshot-version: Set the project to the next snapshot version if it is a non-snapshot version.
:list-projects: List the projects found under the project directory.
:set-release-version: Set the project to the next release version if it is a snapshot version.
:show-release-version: Show the pending release version.
:show-current-version: Show the current project version


arguments
:project-version project-version version to set the project to.  If empty then perform:
if first project.clj version contains snapshot
 - remove snapshot, (if --date-version add date).

else
 - if dated remove
 - else increment last integer
 - add snapshot
:project-directory The top level directory to recursively search for projects.
:date-version Boolean true or false to use the date when release versioning.


think.release[master] % lein run show-release-version --date-version false
0.1.0
think.release[master] % lein run show-release-version --date-version true
0.1.0-2017-05-11-14-24
```

## Interesting Details
* Generates command line parameters from public functions in [core](src/think/release/core.clj) namespace and their doc strings.
* Command line options can be environment variables encoded in project config file [think.config](http://github.com/thinktopic/think.config).
* All of the above is due to machinery in config and [main](src/think/release/main.clj).
## License

Copyright © 2017 ThinkTopic.com, LLC

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
