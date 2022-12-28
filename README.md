# Gitlet
A lite version of Git, a version control system.

Limitations:
1) Gitlet ignores any subdirectories and only works for files made in the directory where Gitlet is initialised. For instance, if Gitlet is initialised in directory "GitletTest", Gitlet only works for files created in "GitletTest". If a subdirectory is made in "GitletTest", Gitlet will ignore the subdirectory.
2) Gitlet will not run any commands that are not listed. See "Commands and operands" for commands that Gitlet can run.
3) Gitlet does not work for any non text-based files such as .doc and .docx files. 

This program requires Java to run.

How to use:  
Copy the gitlet folder to the directory you want Gitlet to run in. Then, use a command line interface and change directory to the directory containing the gitlet folder.
To run a Gitlet command, type the following between the dashed lines:   
------  java gitlet.Main `<COMMAND> <OPERAND1> <OPERAND2> <OPERAND3>` ...  ------  
Do not delete the .gitlet folder as it is used for version control in the directory. If you delete it, every commit made will be lost.  

Commands and operands:
1) init: Initialises Gitlet version control system
2) add [filename]: Adds file to be staged
3) commit [message]: Creates a new commit with given message
4) rm [filename]: Unstages the file if it is staged for addition. If the file is tracked in the current commit, stages it for removal and removes the file from the                         working directory
5) log: Displays information about each commit backwards starting from the head commit
6) global-log: Displays information about every commit (order is not guaranteed)
7) find [message]: Displays the ids of commits that have the given message
8) status: Displays head branch and other branches, staged files, removed files, modifications not staged for commit, and untracked files
9) checkout -- [filename]: Takes the version of the file as it exists in the head commit and puts it in the working directory, overwriting the version of the file that’s                            already there if there is one. The new version of the file is not staged.
10) checkout [commit id] -- [filename]: Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the                                         version of the file that’s already there if there is one. The new version of the file is not staged.
11) checkout [branch]: Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that                        are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files                          that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-                        out branch is the current branch
12) branch [branch]: Creates a new branch and points it at the commit pointed by the current HEAD
13) rm-branch [branch]: Removes branch with given name
14) reset [commit id]: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit, moves the current branch’s head                          to that commit node. The staging area is cleared. This command is the checkout of an arbitrary commit that also changes the current branch head.
15) merge [branch]: Merges the given branch into the current branch (head branch)
