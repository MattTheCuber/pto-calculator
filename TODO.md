## Musts

- [ ] Persistent list of entries
- [ ] Fix bug with adding a second entry to the current date if there isn't enough PTO for the new entry

## Shoulds

- [ ] Add the current PTO balance to the toolbar
- [ ] Add button for adding entry to calendar in toolbar which opens an input dialog
- [ ] Make editing the date or time not close the popover immediately
- [ ] If the current date changes, update userSettings.currentBalance
- [ ] Instead of removing entries when saving settings, let the user decide what to do
- [ ] Write more tests

## Coulds

- [ ] Restrict adding events to only weekdays
- [ ] Enforce configurable increments for durations (e.g., 15 minutes or 8 hours)
- [ ] Make the settings fields more reactive by listening to on type instead of just value changes
- [ ] Add accrual compounding period
- [ ] Package and document process (see here: https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-runnable-jar-with-dependencies-using-maven)
- [ ] Add CI for releases
