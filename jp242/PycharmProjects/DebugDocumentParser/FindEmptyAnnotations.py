__author__ = 'jp242'

import os,shutil

def findEmptyAnnotations(docDir,outputDir):
    files = os.listdir(docDir)
    bugFile = open(outputDir + "/" + "brokenFiles.txt", 'w')
    i = 0
    for f in files:
        doc = open(docDir + "/" + f,'r')
        tokens = doc.read().split("\t")
        i = checkTokens(tokens,docDir + "/" + f,bugFile, i)
        doc.close()
    bugFile.close()
    print i


def checkTokens(tokens, fileName,file,i):
    for token in tokens:
        if token.startswith("_"):
            i += 1
            file.write(token + "\t")
    return i


if __name__ == "__main__":
    docDir = "/Volumes/BackupHD/Projects/Lumi/datasets/full-dataset-pos-ner/consolidated-ner-pos"
    outputDir = "/Volumes/BackupHD/Projects/Lumi/datasets/full-dataset-pos-ner"
    findEmptyAnnotations(docDir,outputDir)