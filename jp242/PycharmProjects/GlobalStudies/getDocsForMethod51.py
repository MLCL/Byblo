__author__ = 'jp242'

import solr,datetime

_solrLoc = "http://localhost:8983/solr"
_outputLoc = ""
_pageSize=200
_fields=['uuidhash','url','content','lang','location','tstamp']

def querySolr(solrLoc=_solrLoc,outputLoc=_outputLoc):
    outputFileName = "response." + "" + ".csv"
    s = solr.SolrConnection(_solrLoc)
    response = s.query("analyst-relevant:false")
    i = 0
    numFound = response.numFound
    nf=0
    while i < numFound:
        response = s.query("analyst-relevant:false",start=i,rows=_pageSize)
        for hit in response.results:
            print hit['uuid']


if __name__ == "__main__":
    querySolr()