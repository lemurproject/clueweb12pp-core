'''
List of vbulletin subforums
'''

import argparse
from vbulletin import vbulletin


if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('vbulletin_list', metavar = 'vbulletin-list', help = 'vbulletin seeds list')
		parser.add_argument(
			'--unfriendly',
			dest = 'unfriendly',
			default = False,
			action = 'store_true',
			help = 'Handle unfriendly URLs'
		)
		parser.add_argument(
			'--friendly-1',
			dest = 'friendly_1',
			default = False,
			action = 'store_true',
			help = 'Handle friendly urls where the index pages are of the form foo.bar/f3/'
		)
		parser.add_argument(
			'--add-slash',
			dest = 'add_slash',
			default = False,
			action = 'store_true',
			help = 'Add a / to the end of the seeds'
		)
		parser.add_argument(
			'--vbseo-1',
			dest = 'vbseo_1',
			default = False,
			action = 'store_true',
			help = 'Handle urls of type /f\d+.html'
		)
		parser.add_argument(
			'--vbseo-2',
			dest = 'vbseo_2',
			default = False,
			action = 'store_true',
			help = 'Handles urls of style board.com/foo/subforum/post_id-post-slug.html'
		)

		return parser.parse_args()

	parsed = parse_cmdline_args()

	with open(parsed.vbulletin_list, 'r') as vbulletin_list_handle:
		for url in vbulletin_list_handle:
			try:
				if parsed.unfriendly:
					print vbulletin.process_unfriendly_subforum_urls(url.strip())

				elif parsed.friendly_1:
					print vbulletin.process_friendly_subforum_urls_1(url.strip())

				elif parsed.add_slash:
					print vbulletin.attach_slash_end(url.strip())

				elif parsed.vbseo_1:
					print vbulletin.vbseo_subforum_urls_1(url.strip())

				elif parsed.vbseo_2:
					print vbulletin.vbseo_subforum_urls_2(url.strip())
			
			except:
				pass