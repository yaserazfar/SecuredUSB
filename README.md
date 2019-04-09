# DIY_USB
241 DIY_USB Project

Key Idea: Turn any USB thumbdrive into a more secure device by combining features of your phone: fingerprint recognition to decode, alert you that the thumbdrive is still in the computer when it looks like you're leaving it behind by mistake.

    Keywords: Mobile App development; Encryption; IoT/iBeacons

While some companies do make USB thumbdrives that require fingerprint authentication to access the data stored on it (for example Lexar, how about all the people that didn't have the forethought to buy such a device, but would like the convenience of being able to add such functionality to the existing USB thumbdrives they already have? Enter DIY USB Thumb-print-drive.

The idea to this project is to take advantage of the fact that most people have smartphones, and those devices support bioinformatic forms of identification, such as fingerprint and face recognition. Hook that up with some software that can detect when the phone and a USB flashdrive have been plugged into the same computer, and the software can effectively make the device operate as if it was one of those fancy fingerprint thumbdrives. Next, through the GPS/location serives on the phone, if the software can detect that the person who owns the phone appears to be moving away without unplugging the USB thumbdrive, it can sound an alert on the phone. Taking this project to the next level would be developing a solution that doesn't require the phone to be plugged in at all to a host PC to have this functionality work!

## Using git
### Useful Commands
- `git checkout <branch_name>`: changes the branch you are working with. Will create a new branch if it doesn't exist

### Pushing Changes to Server
1. `git add <file1> [file2] ...` or `git add .`: saves your changes locally ready to commit
2. `git commit -m "commit message"`: groups all your changes together and gets ready to send them to the server
3. `git push`: sends your commit/s to the server, ready for everyone else to see and use

### Getting Changes from Server
0. `git fetch`: check to see if there are any changes (optional)
1. `git pull`: grab the changes and apply them to your local version

Note that if you have any changes to files that are being changed by the pull, you will have a 'merge conflict', which can be really hard to fix especially if you don't know what you're doing. To make this less likely, you need to 'stash' first. This is somewhat more useful if you don't want to commit any changes yet but need to swap branches (if you don't stash first, your changes will move branches with you):
1. `git stash`: saves your changes to a permanent stack
2. Do other things (`git pull`, `git checkout`, etc)
3. `git stash apply / git stash apply stash@{n}`: restores your latest stash / the specified stash. To find `n`, run `git stash list` to see a list of the stashes.
