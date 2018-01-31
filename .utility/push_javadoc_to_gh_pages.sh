#!/bin/bash

if [ "$TRAVIS_JDK_VERSION" == "oraclejdk8" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
  echo -e "Publishing documentation...\n"

  cp -R $TRAVIS_BUILD_DIR/build/docs/javadoc $HOME/javadoc-latest
  cp -R $TRAVIS_BUILD_DIR/build/reports/findbugs $HOME/findbugs-latest
  cp -R $TRAVIS_BUILD_DIR/build/reports/checkstyle $HOME/checkstyle-latest
  cp -R $TRAVIS_BUILD_DIR/build/reports/jacoco/test/html $HOME/jacoco-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/comfort-framework/comfort gh-pages > /dev/null

  cd gh-pages
  git rm -rf ./javadoc
  git rm -rf ./findbugs
  git rm -rf ./checkstyle
  git rm -rf ./jacoco

  cp -Rf $HOME/javadoc-latest ./javadoc
  cp -Rf $HOME/findbugs-latest ./findbugs
  cp -Rf $HOME/checkstyle-latest ./checkstyle
  cp -Rf $HOME/jacoco-latest ./jacoco

  git add -f .
  git commit -m "Latest documentation on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null

  echo -e "Published documentation to gh-pages.\n"
fi
