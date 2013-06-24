clueweb12pp-core
================

ClueWeb12++ is an extension to the ClueWeb12 Dataset at Carnegie Mellon University. 
This dataset adds an ephemeral layer to the web (blogs and discussion forums) to the archival web (standard web-pages).

Forum Crawling:
---------------

The approach we use for forum crawling is to download a forum's entire index pages and then extract post pages from there.
This approach allows us to cut down on the amount of data we download and the processing power we employ to sieve through
it.

Crawl Infrastructure:
---------------------

Two 64 GB RAM servers at CMU, 1 8 GB RAM machine at CMU and two AWS minimal instances form part of our crawling infrastructure.



Previously we had several projects - now we've got one.

Organization:

        clojure/clueweb12pp_core
                                |
                                | -> /process_name/*.clj   contains code for processing downloaded data from one heritrix job
        
        clojure/warc-clojure/       Contains our wrapper around the java library to read warc files
        
        clojure/heritrix-monitor    Contains code for monitoring heritrix and generating more detailed reports for our downloads
        
        
