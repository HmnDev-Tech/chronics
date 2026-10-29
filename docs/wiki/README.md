# Wiki source

These pages are the source of the GitHub Wiki. They live in the repository so
they can be reviewed, diffed and translated in pull requests.

GitHub does not currently allow enabling the wiki for this private repository.
Once the repository is public (or the plan supports private wikis), publish the
pages:

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
