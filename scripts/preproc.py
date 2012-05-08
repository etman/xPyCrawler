#!/usr/bin/python
import sys, os

SCRIPT_PATH = os.path.abspath(__file__)
WORKING_DIR = SCRIPT_PATH[:SCRIPT_PATH.rindex('/')]

def main(args):
    rawStdIn = sys.stdin.read();
    print rawStdIn

if __name__ == "__main__":
    main(sys.argv[1:]);