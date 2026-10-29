# Wiki source

These pages are the source of the GitHub Wiki. They live in the repository so
they can be reviewed, diffed and translated in pull requests.

The wiki is enabled for this repository, but GitHub does not allow creating the
first wiki page over git — the wiki repository does not exist until the first
page is created in the web UI. Do that once:

1. Open <https://github.com/HmnDev-Tech/chronics/wiki/_new>.
2. Name the page `Home` and paste the contents of `Home.md`.
3. Save it.

After that the pages can be published and updated from the command line:

```bash
./scripts/publish-wiki.sh
```

The script clones `chronics.wiki.git`, copies every page except this README, and
pushes. Re-running it updates the wiki.

## Pages

| File | Wiki page |
| --- | --- |
| `Home.md` | Home |
| `Installation.md` | Installation |
| `Permissions.md` | Permissions |
| `Privacy.md` | Privacy |
| `Troubleshooting.md` | Troubleshooting |
| `Building.md` | Building and signing |
| `_Sidebar.md` | Sidebar navigation |

## Editing rules

- English only, like the rest of the repository.
- No personal data, real notification contents or credentials.
- Keep instructions in sync with the app; update the page in the same pull
  request that changes the behaviour.
- Link to files with absolute GitHub URLs so the links work from the wiki.
