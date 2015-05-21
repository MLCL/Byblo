__author__ = 'jp242'

def findIndividualTokens(file):
    f = open(file,'r')
    tokens = f.read().split('\t')
    for token in tokens:
        if token.startswith("_N") or token.startswith("_V"):
            print token


if __name__ == "__main__":
    findIndividualTokens("/Volumes/BackupHD/Projects/Lumi/datasets/full-dataset-pos-ner/brokenFiles.txt")
