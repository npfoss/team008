'''
## Hephaestus, a system for mass testing your battlecode bot!

In order to use Hephaestus you need python 3.x and Gradle. To run Hephaestus simply navigate to the battlecodeScaffold2017 root directory and run the following command. You may need to specify the python version if you havent set 2.x as your default.

python Hephaestus.py <pkg of team a> <pkg of team b>

flags:
	-x run both ways
	-m allows you to choose the maps by putting them in a space-separated string. does all the maps if this flag is missing

ex: python Hephaestus.py team008.jonahBot team008.defendbot00 -m "Lanes Alone" -x

Hephaestus will then play team a and team b against eachother on all the selected maps (in both positions if you tell it to). It should return a result like the following.

	running on maps:  ['Arena']
	Team A won on Arena
	flipping sides... (but not names: team008.jonahBot is still called team A but plays as blue)
	Team A won on Arena
	Team A won 2 games -- Team B won 0

When the starting positions are flipped the output will keep the original names.

**NOTE** may not work if you print a ton of stuff... it'll also be a lot slower regardless

Enjoy!
'''
import subprocess
import sys

def getMapNames():
	infile = open('maps.txt', 'r') # r for read
	names = [line.strip() for line in infile]
	infile.close()
	return names

def runMatches(teamA, teamB, stats, maps, flipTeams=False):
	for mapName in maps:
		command = ['gradle', 'run', '-PteamA='+teamA, '-PteamB='+teamB, '-Pmaps='+mapName]
		#print(command)
		result = subprocess.check_output(command, shell=True).decode("utf-8")
		winningTeam = result[result.find(') wins')-1]
		if (flipTeams):
			winningTeam  = ('A','B')[winningTeam == 'A']
		stats[winningTeam]+=1
		
		print ('Team ' + winningTeam + ' won on '+mapName)

if __name__ == '__main__':

	mapNames = ''
	teamA = ''
	teamB = ''
	doBothDirs = False
	skipNext = False
	for i in range(1, len(sys.argv)):
		if skipNext:
			skipNext = False
			continue
		if sys.argv[i][0] == '-':
			# flag
			if sys.argv[i][1] == 'x':
				doBothDirs = True
			elif sys.argv[i][1] == 'm':
				skipNext = True
				mapNames = sys.argv[i+1].split()
		elif teamA:
			teamB = sys.argv[i]
		else:
			teamA = sys.argv[i]

	if not mapNames:
		mapNames = getMapNames()
	print("running on maps: ", mapNames, "both ways" if doBothDirs else "only one way")
	gameStats = {'A': 0, 'B': 0} #index zero for team A index one for Team B
	
	#Play all the maps
	runMatches(teamA, teamB, gameStats, mapNames)
	if doBothDirs:
		#Play all maps with reverse spots
		print("flipping sides... (but not names: %s is still called team A but plays as blue)"%teamA)
		runMatches(teamB, teamA, gameStats, mapNames, True)
	
	#Print the final results
	print('Team A won ' + str(gameStats['A']) +' games -- Team B won ' + str(gameStats['B']) )