color 0a
@echo off & setlocal

set var=":sun_with_face::sunflower::palm_tree::house_with_garden::office::octocat::guitar::meat_on_bone:"
set d=%date:~0,10%
set t=%time:~0,8%
set m="springboot版本升级到2.1.3"

git add .
git commit -am "%d% %t% ____ %var% %m%"
git push origin master