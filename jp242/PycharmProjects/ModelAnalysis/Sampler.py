__author__ = 'jp242'

import os

def sample(filelist, fileDir, outDir):
    fList = open(filelist,'r')
    line = fList.readline()
    fDict = dict()
    while line:
        id = line.replace("\n","")
        line = fList.readline()
        docs = line.split("\t")
        os.makedirs(outDir + "/" + str(id))
        for doc in docs:
            docVal = doc.replace("\n","")
            if docVal:
                file = open(fileDir + "/" + docVal)
                out = open(outDir + "/" + str(id) + "/" + docVal,'w')
                out.write(file.read())
                out.close()
                file.close()
        line = fList.readline()
    fList.close()


if __name__ == "__main__":
    fileList = "/Volumes/BackupHD/Projects/Lumi/NER-50/fileList/entity-top-doc.txt"
    fileDir = "/Volumes/BackupHD/Projects/Lumi/NER-50/inputDocs/parsed"
    outDir = "/Volumes/BackupHD/Projects/Lumi/NER-50/topic-docs"
    sample(fileList,fileDir,outDir)

