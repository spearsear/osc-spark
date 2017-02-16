##################################
#
#  Simulate streaming files for testing of spark streaming fraud-detection
#
#  Usage: python streamify.py 201608161500.pcdr 201608161510.pcdr
#         python psrc/streamify.py pcdrs/0/201608101210.pcdr pcdrs/0/201608101220.pcdr pcdrs/0/201608101220.pcdr pcdrs/0/201608101240.pcdr | nc -lk 7979
#         python psrc/streamify.py pcdrs/0/test0.pcdr pcdrs/0/test1.pcdr pcdrs/0/test2.pcdr| nc -lk 7979
#
##################################

import sys
import time
import random

def echo(l):
    return l

def streamify(f):
    def helper(l):
        for x in l:
            time.sleep(random.random())
            yield f(x)
    return helper

def compose(f,g):
    def helper(x):
        return f(g(x))
    return helper

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: streamify.py file1.csv file2.csv ...")
        exit(-1)
    lines_of_file = compose(streamify(echo),open)
    for f in sys.argv[1:]:
        for l in lines_of_file(f):
            print l.replace("\"","")
            #sys.stdout.flush()
