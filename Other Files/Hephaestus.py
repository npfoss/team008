import commands
import sys
if __name__ == '__main__':

	mapNames = ['shrine','Barrier','DenseForest','Enclosure','Hurdle','SparseForest']
	teamA = sys.argv[1]
	teamB = sys.argv[2]
	gameStats = {'A': 0, 'B': 0} #index zero for team A index one for Team B
	
	#Play all the maps
	for mapName in mapNames:
		command = 'gradle run -PteamA='+teamA+' -PteamB='+teamB+' -Pmaps='+mapName
		result = commands.getoutput(command)
		#print result
		winIndex = result.find('wins')
		winningTeam = result[winIndex-3:winIndex-2]
		gameStats[winningTeam]+=1
		
		print ('Team ' + winningTeam + ' won on '+mapName)
	#Play all maps with reverse spots
	for mapName in mapNames:
		command = 'gradle run -PteamA='+teamB+' -PteamB='+teamA+' -Pmaps='+mapName
		result = commands.getoutput(command)
		#print result
		winIndex = result.find('wins')
		winningTeam = result[winIndex-3:winIndex-2]
		winningTeam  = ('A','B')[winningTeam == 'A']
		gameStats[winningTeam]+=1
		print ('Team ' + winningTeam + ' won on '+mapName+' with positions flipped') 
	
	#Print the final results
	print('Team A won ' + str(gameStats['A']) +' games -- Team B won ' + str(gameStats['B']) )
