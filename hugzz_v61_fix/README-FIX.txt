HugzzBotzX V6.1 - ActivityManager import fix

Root cause confirmed from GitHub Actions run 41165968865:
MainActivity.java uses ActivityManager.MemoryInfo in deviceSummary(),
but the class was not imported. Java compiler reports:
  package ActivityManager does not exist
  cannot find symbol: class ActivityManager

Apply from the repository root (~/projects/HugzzBotzX):
  bash fix-activitymanager.sh

Then:
  git add app/src/main/java/com/hugzzbotzx/app/MainActivity.java
  git commit -m "Fix missing ActivityManager import"
  git push origin main

This patch only adds the required Android framework import. It does not alter app behavior.
