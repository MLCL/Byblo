from collections import defaultdict
from operator import itemgetter
import os,shutil

def getTopicDocs(numDocs,location):
    model = open(location, 'r')
    topics = defaultdict(list)
    line = model.readline()
    doc = 0
    while line:
        i = 0
        line = line.replace("\n","")
        probs = line.split()
        while i < len(probs):
            topics[i].append([doc,float(probs[i])])
            i += 1
        doc += 1
        line = model.readline()
    for topic in topics.keys():
        topics[topic] = sorted(topics[topic],key=itemgetter(1),reverse=True)[:numDocs]
        print topics[topic]

    return topics

def getDocs(topicdocs,index,docLoc,outDir):
    outDir = outDir+"/"+"top-docs-16072014-basicLDA"
    if not os.path.exists(outDir):
        os.makedirs(outDir)
    indexDict = dict()
    indx = open(index,'r')
    line = indx.readline()
    while line:
        tup = line.split()
        indexDict[int(tup[0])] = tup[1]
        line = indx.readline()
    indx.close()

    for key in topicdocs.keys():
        topicout = outDir + "/" + str(key)
        if not os.path.exists(topicout):
            os.makedirs(topicout)
        docIds = topicdocs[key]
        for id in docIds:
            f = indexDict[id[0]]
            shutil.copyfile(docLoc + "/" + f,topicout + "/" + f)


if __name__ == "__main__":
    numDocs = 10
    location = "/Volumes/BackupHD/Phd-LDA/JGibbOutput/withSig-func/model-final.theta.txt"
    topicDocs  = getTopicDocs(numDocs,location)
    indexLoc = "/Volumes/BackupHD/Phd-LDA/JGibbOutput/func-tfidf-index.txt"
    docLoc = "/Volumes/BackupHD/Phd-LDA/JGibbOutput/tfidf"
    outDir = "/Volumes/BackupHD/Phd-LDA/JGibbOutput/withSig-func"
    getDocs(topicDocs,indexLoc,docLoc,outDir)
