'''
Code for handling the phpbb section of the corpus
'''

import urlparse

def process_unfriendly_forum_urls(url):
	'''
	Process URLS of the form:
	www.board.com/index.php?showforum=id

	We strip off the extraneous query info and leave in just the subforum id
	'''
	parsed = urlparse.urlparse(url)
	import ipdb
	ipdb.set_trace()