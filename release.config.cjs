module.exports = {
  branches: ["main"],
  repositoryUrl: "https://github.com/suriyadi15/qrishook.git",
  tagFormat: "v${version}",
  plugins: [
    [
      "@semantic-release/commit-analyzer",
      {
        releaseRules: [
          { type: "feat", release: "minor" },
          { type: "fix", release: "patch" },
          { type: "perf", release: "patch" },
          { breaking: true, release: "major" },
        ],
      },
    ],
    "@semantic-release/release-notes-generator",
    [
      "@semantic-release/exec",
      {
        prepareCmd:
          "ANDROID_VERSION_NAME=${nextRelease.version} ./gradlew testDebugUnitTest assembleRelease && mkdir -p release && cp app/build/outputs/apk/release/app-release.apk release/qrishook-v${nextRelease.version}-release.apk",
      },
    ],
    [
      "@semantic-release/github",
      {
        assets: [
          {
            path: "release/*.apk",
            label: "Signed release APK",
          },
        ],
      },
    ],
  ],
};
