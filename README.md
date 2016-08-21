[![Build Status](https://travis-ci.org/pheymann/scala.bft.svg?branch=develop)](https://travis-ci.org/pheymann/scala.bft)

# Scala.bft
This projects aims to provide scala based implementation of the **B**yzantine **F**ault **T**olerance protocole and furthermore to introduce a **mutli-leader** approach to obtain parallelism and an improved performance.

A short introduction and description of the general idea of how to enable multiple leader is given in the [Wiki](https://github.com/pheymann/scala.bft/wiki).

Check out the current feature branches and issues to see what is currenlty in development and what comes next.

## Project
 - *bft-replica*: implements the basic **B**yzantine **F**ault **T**olerance protocol for the replicas

## Guideline
If you want to participate and change stuff on this project you have to know the following guidelines:
 - the workflow relies on [git flow](http://danielkummer.github.io/git-flow-cheatsheet/)
 - everything should be covered by tests (I know it is boring and even I'm sometimes leave some test cases out, but try your best)
 - as this project uses [Travis CI](https://travis-ci.org/pheymann/scala.bft) for build management it also checks if tests are running; so be sure the tests are okay before creating a PR
