#!/usr/bin/python
import sys, os, StringIO, base64, json
import pycurl

SCRIPT_PATH = os.path.abspath(__file__)
WORKING_DIR = SCRIPT_PATH[:SCRIPT_PATH.rindex('/')]

def is_valid_url(url):
    return True

def init_curl_handle_for(m, url): 
    c = pycurl.Curl()
    buffer = StringIO.StringIO()
    fn = buffer.write
    c.response = buffer

    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.USERAGENT, "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)")
    c.setopt(pycurl.FOLLOWLOCATION, True)
    c.setopt(pycurl.AUTOREFERER, True)
    c.setopt(pycurl.CONNECTTIMEOUT, 90)
    c.setopt(pycurl.TIMEOUT, 90)
    c.setopt(pycurl.SSL_VERIFYHOST, False)
    c.setopt(pycurl.SSL_VERIFYPEER, False)
    c.setopt(pycurl.MAXREDIRS, 10)
    c.setopt(pycurl.NOSIGNAL, True)
    c.setopt(pycurl.HEADER, True)
    c.setopt(pycurl.WRITEFUNCTION, fn)
    m.add_handle(c)
    return [c, url];

def busy_loop(m):
    while 1:
        ret, num_handles = m.perform()
        if ret != pycurl.E_CALL_MULTI_PERFORM: break
    while num_handles:
        ret = m.select(1.0)
        if ret == -1:  continue
        while 1:
            ret, num_handles = m.perform()
            if ret != pycurl.E_CALL_MULTI_PERFORM: break

def process(c, url):
    r = {
        "url": url,
        "status": c.getinfo(pycurl.RESPONSE_CODE),
        "effective_url": c.getinfo(pycurl.EFFECTIVE_URL),
        "total_time": c.getinfo(pycurl.TOTAL_TIME),
        "header_size": c.getinfo(pycurl.HEADER_SIZE),
        "content_type": c.getinfo(pycurl.CONTENT_TYPE),
        "content_type": c.getinfo(pycurl.CONTENT_TYPE),
        "primary_ip": c.getinfo(pycurl.PRIMARY_IP),
        "response": c.response.getvalue()
    }
    headerSize = c.getinfo(pycurl.HEADER_SIZE)
    if headerSize > 0:
        r['header'] = r['response'][:headerSize]
        r['response'] = r['response'][headerSize:]
        
    json.dump(r, sys.stdout)
    print

def dispose(m, c):
    m.remove_handle(c)
    c.close()

def main(args):
    m = pycurl.CurlMulti()
    m.handles = [init_curl_handle_for(m, x) for x in args if is_valid_url(x)]
    try:
        busy_loop(m)
        [process(c, url) for c, url in m.handles]
        [dispose(m, c) for c, url in m.handles]
    finally:
        m.close()

if __name__ == "__main__":
    main(sys.argv[1:]);
