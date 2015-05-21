__author__ = 'jp242'

import os

def findEntities(docDir,docList):
    entities = ["_person","_location","_organization"]
    neDocList = dict()
    for id in docList.keys():
        newDocList = []
        for doc in docList[id]:
            docF = open(docDir + "/" + doc)
            docStr = docF.read()
            exists = False;
            for entity in entities:
                if entity in docStr:
                    exists = True
            if exists:
                newDocList.append(doc)
            docF.close()
        neDocList[id] = newDocList
    return neDocList

def fileListReader(listLoc):
    doc = open(listLoc,'r')
    docList = dict()
    line = doc.readline()
    while line:
        i = line.replace('\n','')
        line = doc.readline()
        docList[i] = line.replace('/','').replace('\n','').split()
        line = doc.readline()
    return docList


def fileListWriter(docList,outfile):
    out = open(outfile,'w')
    for id in docList.keys():
        out.write(id + "\n")
        for doc in docList[id]:
            out.write(doc + "\t")
        out.write("\n")
    out.close()

if __name__ == "__main__":
    listloc = "/Volumes/BackupHD/Projects/Lumi/NER-50/fileList/top-topic-docs.txt"
    docDir= "/Volumes/BackupHD/Projects/Lumi/NER-50/inputDocs/parsed"
    outfile = "/Volumes/BackupHD/Projects/Lumi/NER-50/fileList/entity-top-doc.txt"
    origDocList = fileListReader(listloc)
    newDocList = findEntities(docDir,origDocList)
    fileListWriter(newDocList,outfile)



