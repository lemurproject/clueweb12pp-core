'''
Operate on the seeds
'''


def get_calendar_seed(community_home_uri):
	'''
	Given the base directory of a community, return a link to the 
	2012 calendar date range
	'''
	if community_home_uri.endswith('/'):
		return community_home_uri + '2012/'
	return community_home_uri + '/2012/'

