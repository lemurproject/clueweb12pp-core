'''
Generates a list of subforums
'''

import argparse
from ipboard import ipboard

if __name__ == '__main__':
	def parse_cmdline_args():
		parser = argparse.ArgumentParser()

		parser.add_argument('ipboard_list', metavar = 'ipboard-list', help = 'IP.Board list')
		parser.add_argument(
			'--unfriendly', 
			dest = 'unfriendly', 
			action = 'store_true', 
			default = False, 
			help = 'Build seeds list of unfriendly urls'
		)
		parser.add_argument(
			'--friendly-windows',
			dest = 'friendly_windows',
			action = 'store_true',
			default = False,
			help = 'URLs of type /index.php?/forum/'
		)
		parser.add_argument(
			'--friendly-apache',
			dest = 'friendly_apache',
			action = 'store_true',
			default = False,
			help = 'Urls of type index.php/forum/'
		)
		parser.add_argument(
			'--friendly-mod-rewrite',
			dest = 'friendly_mod_rewrite',
			action = 'store_true',
			default = False,
			help = 'Mod-Rewrite allows us to drop index.php'
		)

		return parser.parse_args()

	parsed = parse_cmdline_args()

	with open(parsed.ipboard_list, 'r') as ipboard_list_handle:
		for url in ipboard_list_handle:
			try:
				if parsed.unfriendly:
					print ipboard.process_unfriendly_subforum_urls(url.strip())
				elif parsed.friendly_windows:
					print ipboard.process_friendly_urls_subforum_windows(url.strip())
				elif parsed.friendly_apache:
					print ipboard.process_friendly_urls_subforum_apache(url.strip())
				elif parsed.friendly_mod_rewrite:
					print ipboard.process_friendly_urls_subforum_mod_rewrite(url.strip())
			except:
				pass