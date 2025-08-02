1. [x] Build project structure
2. [x] Build project skeleton
3. [x] Design GUI
4. [x] Write initial program logic
5. [x] Connect with SQLite database
6. [ ] Finish program logic
7. [ ] Write tests
8. [ ] Package (see here: https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-runnable-jar-with-dependencies-using-maven)

GitHub CI (testing, releases, etc.)

## Musts

- [ ] Make PTO balance pop over work in all views OR disable all views except month
- [ ] Persistent list of entries

## Shoulds

- [ ] Add the current PTO balance to the toolbar
- [ ] Make editing the date or time not close the popover immediately
- [ ] If the current date changes, update userSettings.currentBalance
- [ ] Instead of removing entries when saving settings, let the user decide what to do

## Coulds

- [ ] Restrict adding events to only weekdays
- [ ] Enforce configurable increments for durations (e.g., 15 minutes or 8 hours)
- [ ] Make the settings fields more reactive by listening to on type instead of just value changes
- [ ] Add accrual compounding period
