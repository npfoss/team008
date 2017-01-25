'''
## Hephaestus, a system for mass testing your battlecode bot!

In order to use Hephaestus you need python 3.x and Gradle. To run Hephaestus simply navigate to the battlecodeScaffold2017 root directory and run the following command. You may need to specify the python version if you havent set 2.x as your default.

python Hephaestus.py <pkg of team a> <pkg of team b>

ex: python Hephaestus.py team008.finalBot team008.defendbot00

Hephaestus will then play team a and team b against eachother on all the maps in both positions. It should return a result like the following.

	running on maps:  ['Arena']
	Team A won on Arena
	flipping sides... (but not names: team008.finalBot is still called team A but plays as blue)
	Team A won on Arena
	Team A won 2 games -- Team B won 0

When the starting positions are flipped the output will keep the original names.

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

	mapNames = getMapNames()
	print("running on maps: ", mapNames)
	teamA = sys.argv[1]
	teamB = sys.argv[2]
	gameStats = {'A': 0, 'B': 0} #index zero for team A index one for Team B
	
	#Play all the maps
	runMatches(teamA, teamB, gameStats, mapNames)

	#Play all maps with reverse spots
	print("flipping sides... (but not names: %s is still called team A but plays as blue)"%teamA)
	runMatches(teamB, teamA, gameStats, mapNames, True)
	
	#Print the final results
	print('Team A won ' + str(gameStats['A']) +' games -- Team B won ' + str(gameStats['B']) )