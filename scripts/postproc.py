#!/usr/bin/python
import sys, os, json
from pybloom import ScalableBloomFilter

SCRIPT_PATH = os.path.abspath(__file__)
WORKING_DIR = SCRIPT_PATH[:SCRIPT_PATH.rindex('/')]

def extract_links(fetchedUrl):
    pass

def url_filter(urlLinks):
    pass

def main(args):
    seenUrlSet = ScalableBloomFilter(mode=ScalableBloomFilter.SMALL_SET_GROWTH)
    for ln in sys.stdin:
        if not ln: continue
        fetchedUrl = json.loads(ln)
        
        # continue if we've seen this url already.
        if fetchedUrl['url'] in seenUrlSet or fetchedUrl['effective_url'] in seenUrlSet: continue
        
        # add unseen url to the url set
        seenUrlSet.add(fetchedUrl['url'])
        seenUrlSet.add(fetchedUrl['effective_url'])
        
        # extract links and filter out some urls by url filter.
        outlinks = url_filter(extract_links(fetchedUrl))
        
        # analyze
        
        print "[postproc]%s" % fetchedUrl['url']

if __name__ == "__main__":
    main(sys.argv[1:]);